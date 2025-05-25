LOCAL_ENV = {
  "TYPE": "LOCAL",
  "TABLE_USER": "curating_users",
  "TABLE_PROFILE": "curating_users",
  "TABLE_POST": "curating_posts",
  "TABLE_CATEGORY": "curating_categories",
  "TABLE_INTEREST": "curating_interests",
  "TABLE_INTERACTION": "curating_interactions",
  "TABLE_FOLLOW": "curating_follows",
  "TABLE_LIKE": "curating_likes",
  "COLUMN_USER": "user_id",
  "COMMENT": "comments",
  "WRITER_ID": "writer_id",
}

# 배포용 환경
DEPLOY_ENV = {
  "TYPE": "DEPLOY",
  "TABLE_USER": "users",
  "TABLE_PROFILE": "profiles",
  "TABLE_POST": "posts",
  "TABLE_CATEGORY": "categories",
  "TABLE_INTEREST": "interest_categories",
  "TABLE_INTERACTION": "curating_data",
  "TABLE_FOLLOW": "follows",
  "TABLE_LIKE": "post_reactions",
  "COLUMN_USER": "profile_id",
  "COMMENT": "comments",
  "WRITER_ID": "profile_id",
}

ENV = DEPLOY_ENV

table_user = ENV["TABLE_USER"]
table_profile = ENV["TABLE_PROFILE"]
table_post = ENV["TABLE_POST"]
table_category = ENV["TABLE_CATEGORY"]
table_interest = ENV["TABLE_INTEREST"]
table_interaction = ENV["TABLE_INTERACTION"]
table_follow = ENV["TABLE_FOLLOW"]
table_like = ENV["TABLE_LIKE"]
table_comment = ENV["COMMENT"]
column_user = ENV["COLUMN_USER"]
writer_id = ENV["WRITER_ID"]

# 모든 유저 쿼리
SELECT_ALL_USER = f"""
  select *
  from {table_user}
""" if ENV["TYPE"] == "LOCAL" else f"""
  select {table_profile}.*, {table_user}.gender, YEAR(CURDATE()) - YEAR(STR_TO_DATE(birth_date, '%%Y-%%m-%%d')) - (DATE_FORMAT(CURDATE(), '%%m%%d') < DATE_FORMAT(STR_TO_DATE(birth_date, '%%Y-%%m-%%d'), '%%m%%d')) AS age
  from {table_user}, {table_profile}
  where {table_user}.id = {table_profile}.user_id
"""

# 모든 게시글
SELECT_ALL_POST = f"""
  select {table_post}.*
  from {table_post}
"""

# 모든 게시글
SELECT_ALL_POST_WITH_INTETESTS = f"""
  select {table_post}.id, {table_post}.{writer_id}, {table_post}.content, {table_post}.created_at
  from {table_post}, {table_category}
  where {table_post}.category_id = {table_category}.id
"""

# 모든 카테고리
SELECT_ALL_CATEGORY = f"""
  select *
  from {table_category}
"""

# 모든 관심사
SELECT_ALL_INTEREST = f"""
  select *
  from {table_interest}
"""

# 모든 상호작용
SELECT_ALL_INTERACTION = f"""
  select *, {column_user} as user_id
  from {table_interaction}
"""

# 모든 좋아요
SELECT_ALL_LIKE = f"""
  select *
  from {table_like}
"""

# 모든 댓글
SELECT_ALL_COMMENT = f"""
  select *
  from {table_comment}
"""

# 점수만 가지고 큐레이팅
SELECT_POST_BY_SCORE_IDS = f"""
  select {table_post}.*, "점수" as curated_by
  from {table_post}
  where id in %(score_ids)s
  order by rand()
  limit %(score_num)s
"""

# 팔로우 목록 큐레이팅
SELECT_FOLLOWING_POST = f"""
  select {table_post}.*, "팔로우" as curated_by
  from {table_post}
  where {writer_id} in (
    select following_id
    from {table_follow}
    where follower_id = %(user_id)s
  )
  and id not in %(filter)s
  order by RAND()
  limit %(follow_num)s
"""

# 팔로우 & 점수 기반 큐레이팅
SELECT_POST_BY_SCORE_IDS_AND_FOLLOWING = f"""
  (
    (
      {SELECT_FOLLOWING_POST}
    ) 
    union all
    (
      {SELECT_POST_BY_SCORE_IDS}
    ) 
  )
  limit %(total_num)s
"""

# id 목록 게시글 불러오기
SELECT_POST_BY_IDS = f"""
  select *
  from {table_post}
  where id in %(ids)s
"""

# 사용자의 좋아요 상호작용 불러오기
SELECT_LIKE_BY_USER_ID = f"""
  select *
  from {table_interaction}
  where {column_user} = %(user_id)s and type = 'L'
  order by str_to_date(created_at, '%%Y-%%m-%%d %%H:%%i:%%s') desc
  limit %(num)s
""" if ENV["TYPE"] == "LOCAL" else f"""
  select *
  from {table_like}
  where {column_user} = %(user_id)s
  and positive = 1
  order by str_to_date(updated_at, '%%Y-%%m-%%d %%H:%%i:%%s') desc
  limit %(num)s
"""

# 궁극의 쿼리
# user_id : 큐레이팅 받을 유저 ID
# filter : 제외할 게시글
# follow_num : 팔로우 글 중 몇개 뽑을 지
# remain_num : 나머지 게시글 몇 개를 뽑을 지

# 우선 팔로우 목록에서 follow_num 개를 뽑아주고
# 지금까지 뽑았던 모든 게시글 제외시키면서
# 최신글 & 인기글 (짧은 시간 내에 좋아요가 많은 글 좋아요 테이블 탐색해서(현재시간 - 좋아요 받은 시간 <= 일정수치))을 뽑아서
# 두개 합쳐서 반환


SELECT_POST_BY_RECENT_POPULARITY = f"""

  -- 최신글 & 인기글 뽑기
  -- 최신글 = 3일 안에 작성된 글
  -- 인기글 = 7일 동안 좋아요를 많이 받은 글
  select selected_posts.*
  from
  (
    (
      select {table_post}.*, "최신" as curated_by
      from {table_post}
      where id not in %(filter)s
      order by str_to_date(created_at, '%%Y-%%m-%%d %%H:%%i:%%s') desc
      limit %(candidate_num)s
    )
    union all
    (
      select {table_post}.*, "인기" as curated_by
      from {table_post}
      right outer join
      (
        select post_id
        from {table_like}
        where str_to_date(updated_at, '%%Y-%%m-%%d %%H:%%i:%%s') >= now() - interval 7 day
        and post_id not in %(filter)s
        and positive = 1
        group by post_id
        order by count(*) desc
        limit %(candidate_num)s
      ) as popular_ids
      on {table_post}.id = popular_ids.post_id
    )
  ) as selected_posts
  order by rand()
  limit %(remain_num)s

"""

# 단순 최신 글 뽑기
SELECT_RECENT_POST = f"""
  select *, "최신글 순서대로" as curated_by
  from {table_post}
  where id not in %(filter)s
  order by str_to_date(created_at, '%%Y-%%m-%%d %%H:%%i:%%s') desc
  limit %(num)s
"""

# 게시글 이미지 뽑기
SELECT_POST_IMAGE_URL = f"""
  select image as url, image_order as `order`
  from post_images
  where post_id = %(post_id)s
  order by image_order
"""