
let sliderPosX;
let isChronicleResizing;
let chronicleElement;
let formElement;

window.onload = function () {
	
    console.log('Dokument geladen');
    chronicleElement = document.querySelector('.imixs-workitem > .imixs-workitem-chronicle');
	formElement=document.querySelector('.imixs-workitem > .imixs-workitem-form');
	
    const sliderElement = document.querySelector('.imixs-workitem > .imixs-slider');
	sliderElement.addEventListener('mousedown', (e) => {
	  isChronicleResizing = true;
	  sliderPosX = e.clientX;
	  console.log('slider posX = '+ sliderPosX);
	});
	window.addEventListener('mouseup', (e) => {
	  isChronicleResizing = false;
	});
	window.addEventListener('mousemove', (e) => {
	  if (!isChronicleResizing) return;
	  
	  const parent=formElement.parentElement;
	  console.log('parent width = '+ parent.offsetWidth);
	  console.log('slider posX = '+ sliderPosX);
	  console.log('current width='+formElement.offsetWidth);
	  let _newWidth=formElement.offsetWidth + (e.clientX - sliderPosX);
	  console.log('new width='+_newWidth);
	  

      formElement.style.width =  `${_newWidth}px`; 
       // formElement.setAttribute('style', `${_newWidth}px`);
        formElement.style.flexBasis = `${_newWidth}px`;

  	  // chronicleElement.style.width =  `${parent.offsetWidth-formElement.offsetWidth-60}px`; 
	  
	  
	  sliderPosX = e.clientX;
	 
	});
	        
}
    