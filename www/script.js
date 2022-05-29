var json={}
function sendToServer(directory){
	var request="";
	var empty=true;
	for(var i in json){
		empty=false;
	}
	if(!empty){
		request+="?json="+JSON.stringify(json);
	}
	window.open("./"+directory+request,"_self");
}