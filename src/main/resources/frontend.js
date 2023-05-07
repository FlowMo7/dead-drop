"use strict";

function sendDrop(data) {
    let baseUrl = window.location.protocol + '//' + window.location.host + window.location.pathname;
    let postUrl = baseUrl + 'api/drop'
    encryptAndPostDrop(
        postUrl,
        data,
        function (pickupUrl, password) {
            document.getElementById('error_message').style.display = 'none';
            document.getElementById('drop_content').value = '';
            showDropLink(pickupUrl, password);
        },
        function (statusCode) {
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

function htmlEncode(raw) {
    return raw.replace(/[\u00A0-\u9999<>\&]/g, function (i) {
        return '&#' + i.charCodeAt(0) + ';';
    });
}

function getDrop(id, password) {
    let baseUrl = window.location.protocol + '//' + window.location.host + (window.location.pathname.substring(0, window.location.pathname.indexOf('pickup')));
    let url = baseUrl + 'api/drop/' + id
    fetchDropAndDecrypt(
        url,
        password,
        function (data) {
            document.getElementById('drop_content').innerHTML = htmlEncode(data);
            document.getElementById('drop_content_section').style.display = 'block';
            document.getElementById('container_get_drop').style.display = 'none';
        },
        function () {
            document.getElementById('drop_content_section').style.display = 'none';
            document.getElementById('container_get_drop').style.display = 'none';
            document.getElementById('error_text').style.display = 'block';
        }
    );
}