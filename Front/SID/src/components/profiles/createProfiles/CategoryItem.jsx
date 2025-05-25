const CategoryItem = ({ id, name, checkCategoryHandler, checked }) => {
	const handleChange = (e) => {
		checkCategoryHandler(id, e.target.checked)
	}

	return (
		<div
			className={
				checked ? "bg-rose-100 flex justify-center py-2" : "flex justify-center py-2"
			}
		>
			<input
				type="checkbox"
				name={name}
				id={`category-${id}`} // 고유한 id 생성
				className="w-20 appearance-none bg-[url('/buttonIcon/noCheckedIcon.png')] checked:bg-[url('/buttonIcon/checkedIcon.png')] bg-no-repeat bg-center bg-contain"
				checked={checked}
				onChange={handleChange}
			/>
			<label htmlFor={`category-${id}`} className="w-50 text-xl">
				{name}
			</label>
		</div>
	)
}

export default CategoryItem
