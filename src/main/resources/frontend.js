"use strict";

function sendDrop(apiBaseUrl, data) {
    encryptAndPostDrop(
        apiBaseUrl,
        data,
        function(pickupUrl, password) {
            document.getElementById('error_message').style.display = 'none';
            document.getElementById('drop_content').value = '';
            showDropLink(pickupUrl, password);
        },
        function(statusCode) {
            document.getElementById('error_message').style.display = 'block';
        }
    );
}

function showDropLink(pickupUrl, password) {
    document.getElementById('send_div').style.display = 'none';
    document.getElementById('link_div').style.display = 'block';
    document.getElementById('drop_share_link').innerHTML = pickupUrl;
    document.getElementById('drop_share_password').innerHTML = password;
}

function getDrop(apiBaseUrl, id, password) {
    fetchDropAndDecrypt(
        apiBaseUrl,
        id,
        password,
        function(data) {
            document.getElementById('drop_content').innerHTML = data;
            document.getElementById('drop_content_section').style.display = 'block';
            document.getElementById('container_get_drop').style.display = 'none';
        },
        function() {
            document.getElementById('drop_content_section').style.display = 'none';
            document.getElementById('container_get_drop').style.display = 'none';
            document.getElementById('error_text').style.display = 'block';
        }
    );
}