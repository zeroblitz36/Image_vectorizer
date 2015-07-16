function helloWorld(){
    alert("Hello world!");
}
function startRead() {
    // obtain input element through DOM
    var file = document.getElementById('input').files[0];
    if(file){
        getAsText(file);
    }else{
        alert("You fuked up");
    }
}

function getAsText(readFile) {
    document.getElementById("progress").innerHTML = "File loading...";
    var reader = new FileReader();

    // Handle progress, success, and errors
    reader.onprogress = updateProgress;
    reader.onload = loaded;
    reader.onerror = errorHandler;

    // Read file into memory as UTF-8
    reader.readAsText(readFile, "UTF-8");
}

function updateProgress(evt) {
    if (evt.lengthComputable) {
    // evt.loaded and evt.total are ProgressEvent properties
    var loaded = (evt.loaded / evt.total);
    if (loaded < 1) {
      // Increase the prog bar length
      // style.width = (loaded * 200) + "px";
      document.getElementById("progress").innerHTML = loaded+" % ";
    }
  }
}

function loaded(evt) {

    // Obtain the read file data
    var fileString = evt.target.result;

    document.getElementById("progress").innerHTML = "File content : "+fileString;

    // Handle UTF-16 file dump
    if(utils.regexp.isChinese(fileString)) {
        //Chinese Characters + Name validation
    }
    else {
        // run other charset test
    }
    // xhr.send(fileString)
}

function errorHandler(evt) {
    if(evt.target.error.name == "NotReadableError") {
        // The file could not be read
        document.getElementById("progress").innerHTML = "ERROR: could not read file";
    }
}