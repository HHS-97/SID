import { useEffect, useState } from "react"
import { motion } from "framer-motion"

const NUM_FLOWERS = 20 // 벚꽃 개수

// 랜덤 값 생성 함수
const random = (min, max) => Math.random() * (max - min) + min

const CherryBlossom = ({ id }) => {
	const startX = random(0, window.innerWidth) // 시작 X 위치
	const duration = random(5, 10) // 낙하 시간
	const size = random(20, 50) // 벚꽃 크기
	const delay = random(0, 5) // 애니메이션 지연

	return (
		<motion.div
			initial={{ y: -100, x: startX, opacity: 0 }}
			animate={{
				y: window.innerHeight + 100,
				x: startX + random(-100, 100), // 바람에 흔들리는 효과
				opacity: 1,
				rotate: [0, random(-30, 30), random(-60, 60), 0], // 회전 효과
			}}
			transition={{
				duration,
				delay,
				repeat: Infinity, // 무한 반복
				repeatType: "loop",
				ease: "linear",
			}}
			className="absolute"
			style={{
				width: `${size}px`,
				height: `${size}px`,
			}}
		>
			🌸
		</motion.div>
	)
}

export default function FallingCherryBlossom() {
	const [flowers, setFlowers] = useState([])

	useEffect(() => {
		setFlowers(new Array(NUM_FLOWERS).fill(0))
	}, [])

	return (
		<div className="fixed top-0 left-0 w-full h-full pointer-events-none">
			{flowers.map((_, i) => (
				<CherryBlossom key={i} id={i} />
			))}
		</div>
	)
}
