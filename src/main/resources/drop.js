"use strict";

function encryptAndPostDrop(plainData, onComplete) {
    var generatedPassword = generateStringSequence(16);
    var encrypted = sjcl.encrypt(generatedPassword, plainData);

    post('/api/drop', encrypted, function(data) {
        onComplete(data.pickupUrl, generatedPassword);
    });
}

function fetchDropAndDecrypt(id, password, onLoaded, onError) {
    get('/api/drop/' + id, function(statusCode, data) {
        if (statusCode == 200) {
            try {
                var decrypted = sjcl.decrypt(password, data);
                onLoaded(decrypted);
            } catch(e) {
                onError();
            }
        } else {
            onError();
        }
    });
}

function generateStringSequence(length) {
    var result           = '';
    var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

function post(path, content, onComplete) {
    var request = new XMLHttpRequest();
    request.open("POST", path, true);
    request.setRequestHeader('Content-Type', 'application/json');
    request.onload = function() {
        onComplete(JSON.parse(this.responseText));
    };
    request.send(content);
}

function get(path, onComplete) {
    var request = new XMLHttpRequest();
    request.open("GET", path, true);
    request.onload = function() {
        onComplete(this.status, this.responseText);
    };
    request.send();
}