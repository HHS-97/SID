import { useEffect, useState } from "react"

const Terms = ({ setAllowPersonalTerms, setAllowServiceTerms }) => {
	const [terms, setTerms] = useState({ personal: "", service: "" })
	useEffect(() => {
		setTerms({
			personal: `1. 목적  
- 회원 관리(회원가입, 본인 확인, 서비스 이용 및 상담 등)  
- 맞춤형 게시글 추천 및 서비스 개선을 위한 데이터 분석

2. 수집하는 개인정보 항목  
- 필수 항목:  
  - 이메일  
  - 이름  
  - 생년월일  
  - 전화번호  
  - 성별  
- 서비스 이용 관련 항목:  
  - 사용자 행동 데이터 (글 작성, 조회, 상호작용 이력 등)

3. 개인정보의 수집 및 이용 기간  
- 원칙적으로 회원 탈퇴 시까지 보유 및 이용하며, 관련 법령에 따라 일정 기간(예: 계약 또는 청약철회 등에 관한 기록, 대금결제 및 재화 등의 공급에 관한 기록 등) 보관될 수 있습니다.

4. 개인정보의 제3자 제공  
- 원칙적으로 이용자의 사전 동의 없이 제3자에게 제공하지 않습니다. 단, 법령에 의한 요청이 있거나, 서비스 제공을 위해 필요한 경우 별도의 동의를 받는 절차를 진행합니다.

5. 이용자의 권리 및 행사 방법  
- 이용자는 언제든지 개인정보 열람, 수정, 삭제, 처리 정지 등의 권리를 행사할 수 있으며, 관련 문의는 [S12P11C110_SID]로 연락주시기 바랍니다.

6. 동의 거부 및 불이익  
- 개인정보 제공에 동의하지 않을 경우 회원 가입 및 일부 서비스 이용에 제한이 있을 수 있습니다.`,
			service: `1. 목적  
본 약관은 귀하가 당사(S12P11C110_SID)가 제공하는 서비스를 이용함에 있어 필요한 제반 사항(권리, 의무, 책임 등)을 규정함을 목적으로 합니다.

2. 서비스 제공 내용  
- 회원 가입 및 서비스 이용  
- 이용자의 개인정보(이메일, 이름, 생년월일, 전화번호, 성별, 행동 데이터 등)를 기반으로 맞춤형 게시글 추천 서비스 제공  
- 기타 부가 서비스 제공

3. 회원의 의무  
- 정확한 개인정보 제공 및 최신 정보 유지  
- 서비스 이용 시 관련 법령 및 본 약관 준수  
- 타인의 권리 침해나 부정 사용 행위 금지

4. 서비스 제공자의 의무  
- 안정적이고 지속적인 서비스 제공  
- 이용자의 개인정보 보호를 위한 기술적, 관리적 조치 이행  
- 서비스 이용 관련 문의에 대한 신속한 대응

5. 책임의 제한  
- 당사는 기술적 문제, 데이터 오류 등으로 인한 서비스 중단 및 손해에 대해 관련 법령이 정하는 한도 내에서 책임을 부담합니다.  
- 다만, 고의 또는 중대한 과실이 있는 경우에는 예외로 합니다.

6. 약관의 변경  
- 당사는 필요에 따라 본 약관의 일부 또는 전부를 변경할 수 있으며, 변경된 약관은 사전 공지 또는 개별 통지 후 효력을 발생합니다.

7. 분쟁 해결  
- 본 약관과 관련하여 분쟁이 발생할 경우, 관련 법령에 따라 해결하며, 관할 법원은 [광주광역시]의 관할 법원으로 합니다.

8. 기타  
- 회원 가입 시 본 약관에 동의한 것으로 간주되며, 서비스 이용 중 약관 내용에 대한 변경이 있을 경우 공지사항 등을 통해 안내합니다.`,
		})
		// axios
		// 	.get(import.meta.env.VITE_BASE_URL + "/terms")
		// 	.then((res) => {
		// 		//console.log(res.data)
		// 		setTerms(res.data)
		// 	})
		// 	.catch((err) => {
		// 		//console.log(err)
		// 	})
	}, [])
	return (
		<div>
			<div className="mb-4">
				<h1 className="font-bold text-xl">개인정보 수집 및 이용 동의서</h1>
				<textarea
					name="terms"
					id="terms"
					className="outline-1 w-[100%] p-2 my-4 rounded-md resize-none"
					rows="15"
					defaultValue={terms.personal}
					style={{ textAlign: "start" }}
					readOnly
				/>
				<div style={{ textAlign: "end" }}>
					<input
						type="checkbox"
						name="allowPersonalTerms"
						id="allowPersonalTerms"
						onChange={(e) => {
							setAllowPersonalTerms(e.target.checked)
						}}
					/>
					<label htmlFor="allowPersonalTerms" className="ms-3">
						개인정보 수집 및 이용에 동의합니다.
					</label>
				</div>
			</div>
			<div className="mb-4">
				<h1 className="font-bold text-xl">이용 약관 동의서</h1>
				<textarea
					name="terms"
					id="terms"
					className="outline-1 w-[100%] p-2 my-4 rounded-md resize-none"
					rows="15"
					defaultValue={terms.service}
					style={{ textAlign: "start" }}
					readOnly
				/>
				<div style={{ textAlign: "end" }}>
					<input
						type="checkbox"
						name="allowServiceTerms"
						id="allowServiceTerms"
						onChange={(e) => {
							setAllowServiceTerms(e.target.checked)
						}}
					/>
					<label htmlFor="allowServiceTerms" className="ms-3">
						이용 약관에 동의합니다.
					</label>
				</div>
			</div>
		</div>
	)
}

export default Terms
