const canvas = document.querySelector('canvas');
const form = document.querySelector('.signature-pad-form');
const clearButton = document.querySelector('.clear-button');

const ctx = canvas.getContext('2d');
let writingMode = false;


const handlePointerDown = (event) => { 
	writingMode = true; 
	ctx.beginPath(); 
	const [positionX, positionY] = getCursorPosition(event); 
	ctx.moveTo(positionX, positionY); 
}


const handlePointerUp = () => { writingMode = false; }

const handlePointerMove = (event) => { 
	if (!writingMode) return
  const [positionX, positionY] = getCursorPosition(event); 
  ctx.lineTo(positionX, positionY); 
  ctx.stroke(); 
 }


const getCursorPosition = (event) => { 
	positionX = event.clientX - event.target.getBoundingClientRect().x; 
	positionY = event.clientY - event.target.getBoundingClientRect().y; 
	return [positionX, positionY]; 
}


ctx.lineWidth = 3; 
ctx.lineJoin = ctx.lineCap = 'round';

form.addEventListener('submit', (event) => {
	event.preventDefault();
	const imageURL = canvas.toDataURL(); 
	const image = document.createElement('img'); 
	image.src = imageURL; 
	image.height = canvas.height; 
	image.width = canvas.width; 
	image.style.display = 'block'; 
	form.appendChild(image); 
	clearPad();
})
const clearPad = () => { 
	ctx.clearRect(0, 0, canvas.width, canvas.height);
 }
clearButton.addEventListener('click', (event) => { 
	event.preventDefault(); clearPad(); 
	});



canvas.addEventListener('pointerdown', handlePointerDown, { passive: true }); 
canvas.addEventListener('pointerup', handlePointerUp, { passive: true }); 
canvas.addEventListener('pointermove', handlePointerMove, { passive: true });



