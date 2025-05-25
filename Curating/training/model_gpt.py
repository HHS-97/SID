import pandas as pd

import tensorflow as tf
from tensorflow.keras.layers import Input, Embedding, Flatten, Multiply, Concatenate, Dense, Dropout, BatchNormalization
from tensorflow.keras.models import Model
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.callbacks import ReduceLROnPlateau, EarlyStopping
from tensorflow.keras.regularizers import l2

import mlflow
import mlflow.keras

print("Num GPUs Available: ", len(tf.config.experimental.list_physical_devices('GPU')))

data = {}

# Constants
INTERACTION_COUNT = 100
BATCH_SIZE = 1024
EPOCHS = 30


LATENT_FEATURES = 32
MLP_EMBEDDING_DIM = 128
DROPOUT_RATE = 0.2
L2_REG = 1e-5
LEARNING_RATE = 0.001

TEST_RATIO = 0.1



# ============================
# 1. 데이터 전처리 함수
# ============================
# input_data : DataFrame
def set_data(input_data):
  global data

  # data에 users가 없다면
  if 'user_data' not in input_data:
    raise ValueError("users 데이터가 없습니다.")
  # post가 없다면
  if 'post_data' not in input_data:
    raise ValueError("post 데이터가 없습니다.")
  # interaction이 없다면
  if 'interaction_data' not in input_data:
    raise ValueError("interaction 데이터가 없습니다.")

  # input_data에서 게시글 추출
  users = input_data['user_data'].copy()
  posts = input_data['post_data'].copy()
  interactions = input_data['interaction_data'].copy()

  # type이 R인 건 없앰
  # interactions = interactions[interactions['type'] != 'R']
  
  # 게시글의 제일 큰 id
  max_post_id = posts['id'].max()
  
  # user의 id로 상호작용을 list로 쌓을 dict 선언
  user_interactions = {user_id: [] for user_id in interactions['user_id']}
  
  # interaction 데이터를 user_id를 key로 하여 post_id와 type을 dict로 쌓음
  for user_id, post_id, interaction_type in interactions[['user_id', 'post_id', 'type']].values:
    user_interactions[int(user_id)].append({
      'post_id': post_id,
      'type': interaction_type
    })
    
  train_interactions = []
  test_interactions = []
  
  print("전처리 시작")
  # 각 유저 dict를 순회
  for user_id, interactions in user_interactions.items():
    refined_interactions = []

    # 2번째부터 조회
    for i in range(2, len(interactions)):

      if interactions[i]['type'] != 'L' and interactions[i]['type'] != 'W':
        continue
      
      # N번째 이전까지의 상호작용을 조회
      for j in range(max(0, i - INTERACTION_COUNT), min(i + INTERACTION_COUNT, len(interactions))):
        if i == j:
          continue

        # 상호작용이 'U'인 경우 0, 'L'인 경오 1, 단순 R은 패스, 그 외의 경우 0.8
        label = 0
        if interactions[j]['type'] == 'D':  # 자세히 보기
          label = 1
        elif interactions[j]['type'] == 'L': # 좋아요
          label = 1
        elif interactions[j]['type'] == 'U': # 싫어요
          label = 0
        elif interactions[j]['type'] == 'R': # 단순 큐레이팅
          label = 0
        elif interactions[j]['type'] == 'W': # 게시글 작성
          label = 1
        elif interactions[j]['type'] == 'C': # 댓글 작성
          label = 1

        # 상호작용을 refined_interactions에 추가
        refined_interactions.append({
          'source_post_id': interactions[j]['post_id'],
          'target_post_id': interactions[i]['post_id'],
          'label': label
        })

    # 중앙부터 짤라서 학습용과 테스트용으로 나눔
    half_idx = int(len(refined_interactions) / 2)
    bound_size = int(len(refined_interactions) * TEST_RATIO)
    left_idx = int(half_idx - bound_size / 2)
    right_idx = int(half_idx + bound_size / 2)
    
    
    train_interactions.extend(refined_interactions)
    test_interactions.extend(refined_interactions[right_idx:])


  if(len(train_interactions) == 0 or len(test_interactions) == 0):
    raise ValueError("상호작용이 부족합니다.")
  
  # df로 변환
  train_interactions = pd.DataFrame(train_interactions)
  test_interactions = pd.DataFrame(test_interactions)
  # print("train:", len(train_interactions))
  # print("test:", len(test_interactions))
  
  # data에 저장
  data = {
    'posts': posts,
    'train': (train_interactions['source_post_id'], train_interactions['target_post_id'], train_interactions['label']),
    'test': (test_interactions['source_post_id'], test_interactions['target_post_id'], test_interactions['label']),
    'num_posts': max_post_id + 1,
  }
  
  return data

# ============================
# 2. NeuMF 모델 구성 함수
# ============================
def build_neumf_model(num_posts, latent_features=LATENT_FEATURES, mlp_embedding_dim=MLP_EMBEDDING_DIM,
                      dropout_rate=DROPOUT_RATE, l2_reg=L2_REG, learning_rate=LEARNING_RATE):
    """
    NeuMF 모델 구성  
    - GMF (Generalized Matrix Factorization) + MLP (Multi-Layer Perceptron)  
    - 출력: sigmoid (추천 점수)
    """
    def create_embedding_layer(input_dim, output_dim, name):
        return Embedding(input_dim=input_dim, output_dim=output_dim,
                         embeddings_initializer='random_normal',
                         embeddings_regularizer=l2(l2_reg),
                         name=name)

    # 입력
    post_input = Input(shape=(1,), dtype='int32', name='post_input')
    target_input = Input(shape=(1,), dtype='int32', name='target_input')

    # --- GMF 부분 ---
    gmf_post_embedding = create_embedding_layer(num_posts, latent_features, 'gmf_post_embedding')(post_input)
    gmf_target_embedding = create_embedding_layer(num_posts, latent_features, 'gmf_target_embedding')(target_input)
    gmf_vector = Multiply()([Flatten()(gmf_post_embedding), Flatten()(gmf_target_embedding)])

    # --- MLP 부분 ---
    mlp_post_embedding = create_embedding_layer(num_posts, mlp_embedding_dim, 'mlp_post_embedding')(post_input)
    mlp_target_embedding = create_embedding_layer(num_posts, mlp_embedding_dim, 'mlp_target_embedding')(target_input)
    mlp_vector = Concatenate()([Flatten()(mlp_post_embedding), Flatten()(mlp_target_embedding)])
    mlp_vector = Dense(128, activation='sigmoid')(mlp_vector)
    mlp_vector = Dropout(dropout_rate)(mlp_vector)
    mlp_vector = Dense(64, activation='sigmoid')(mlp_vector)
    mlp_vector = BatchNormalization()(mlp_vector)
    mlp_vector = Dense(32, activation='sigmoid')(mlp_vector)

    # --- 결합 및 최종 출력 ---
    merged = Concatenate()([gmf_vector, mlp_vector])
    output = Dense(1, activation='sigmoid', name='prediction')(merged)

    model_instance = Model(inputs=[post_input, target_input], outputs=output)
    model_instance.compile(optimizer=Adam(learning_rate=learning_rate),
                           loss='binary_crossentropy', metrics=['accuracy'])
    return model_instance
  
# ============================
# 3. 테스트 함수
# ============================
def test(model_instance):
    global data
    # 전역 변수 data가 이미 set_data(input_data)를 통해 설정되어 있어야 함.
    if not data or 'test' not in data:
        raise ValueError("데이터가 설정되지 않았습니다. 먼저 set_data(input_data)를 호출하세요.")
    
    source_posts, target_posts, labels = data['test']
    
    # 모델 평가
    test_loss, test_accuracy = model_instance.evaluate([source_posts, target_posts], labels, verbose=1)
    print(f"테스트 정확도: {test_accuracy:.4f}, 테스트 손실: {test_loss:.4f}")
    
    return test_accuracy
  
# ============================
# 4. 학습 함수
# ============================
def train():
    global data
    # 전역 변수 data가 이미 set_data(input_data)를 통해 설정되어 있어야 함.
    if not data or 'train' not in data:
        raise ValueError("데이터가 설정되지 않았습니다. 먼저 set_data(input_data)를 호출하세요.")
    
    source_posts, target_posts, labels = data['train']
    num_posts = data['num_posts']
    
    # NeuMF 모델 구성
    latent_features = 16
    mlp_embedding_dim = 64
    dropout_rate = 0.2
    l2_reg = 1e-4
    learning_rate = 0.001
    model_instance = build_neumf_model(num_posts, latent_features, mlp_embedding_dim,
                                         dropout_rate, l2_reg, learning_rate)
    
    # MLflow 실험 설정
    mlflow.set_experiment("NeuMF_Post_Recommendation")
    with mlflow.start_run():
        mlflow.log_param("latent_features", latent_features)
        mlflow.log_param("mlp_embedding_dim", mlp_embedding_dim)
        mlflow.log_param("dropout_rate", dropout_rate)
        mlflow.log_param("l2_reg", l2_reg)
        mlflow.log_param("learning_rate", learning_rate)
        
        # 콜백 설정: 학습률 조정, 조기 종료
        lr_scheduler = ReduceLROnPlateau(
            monitor='val_loss',
            factor=0.5,
            patience=2,
            verbose=1,
            min_lr=1e-5
        )
        
        early_stopping = EarlyStopping(
            monitor='val_loss',
            patience=5,
            restore_best_weights=True,
            verbose=1
        )
        
        # 모델 학습
        history = model_instance.fit(
            [source_posts, target_posts],
            labels,
            batch_size=BATCH_SIZE,
            epochs=EPOCHS,
            validation_split=0.1,
            shuffle=True,
            callbacks=[lr_scheduler, early_stopping],
            verbose=1
        )
        
        # 학습 결과 로깅
        mlflow.log_metric("train_accuracy", history.history['accuracy'][-1])
        mlflow.log_metric("val_accuracy", history.history['val_accuracy'][-1])
        mlflow.log_metric("train_loss", history.history['loss'][-1])
        mlflow.log_metric("val_loss", history.history['val_loss'][-1])
        
        # 모델 저장 (MLflow에도 기록)
        mlflow.keras.log_model(model_instance, "model")
    
    print("학습 완료 및 모델/매핑 정보 저장됨.")
    
    # 테스트 돌려서 정확도 구하기
    test_accuracy = test(model_instance)
    
    return model_instance, data['num_posts']