"use strict";

function encryptAndPostDrop(apiBaseUrl, plainData, onComplete, onError) {
    let generatedPassword = generateStringSequence(16);
    let encrypted = sjcl.encrypt(generatedPassword, plainData);

    post(
        apiBaseUrl + 'drop',
        encrypted,
        function(data) {
            onComplete(data.pickupUrl, generatedPassword);
        },
        function(statusCode) {
            onError(statusCode)
        }
    );
}

function fetchDropAndDecrypt(apiBaseUrl, id, password, onLoaded, onError) {
    get(apiBaseUrl + 'drop/' + id, function(statusCode, data) {
        if (statusCode === 200) {
            try {
                let decrypted = sjcl.decrypt(password, data);
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
    let result           = '';
    const characters     = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let charactersLength = characters.length;
    for (var i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

function post(path, content, onComplete, onError) {
    let request = new XMLHttpRequest();
    request.open("POST", path, true);
    request.setRequestHeader('Content-Type', 'application/json');
    request.onload = function() {
        onComplete(JSON.parse(this.responseText));
    };
    request.onerror = function() {
        onError(0);
    };
    request.onreadystatechange = function () {
        if (request.readyState === XMLHttpRequest.DONE && request.status !== 200) {
            //A HTTP error response has been returned
            onError(request.status);
        }
    }
    request.send(content);
}

function get(path, onComplete) {
    let request = new XMLHttpRequest();
    request.open("GET", path, true);
    request.onload = function() {
        onComplete(this.status, this.responseText);
    };
    request.send();
}