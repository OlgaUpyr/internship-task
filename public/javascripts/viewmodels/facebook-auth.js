(function(d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) return;
    js = d.createElement(s);
    js.id = id;
    js.src = 'https://connect.facebook.net/en_US/sdk.js#xfbml=1&version=v2.12&appId=153285488716408&autoLogAppEvents=1';
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));

function fbLogin() {
    FB.getLoginStatus(function (response) {
        if (response.status === 'connected') {
            getFbUserData();
        } else if (response.status === 'not_authorized') {
            FB.login(function (response) {
                if (response.authResponse) {
                    getFbUserData();
                }
            });
        }
    });
}

function getFbUserData(){
    var e = document.getElementById("role");

    FB.api('/me', {locale: 'en_US', fields: 'id,first_name,last_name,email,picture'},
        function (response) {
            var data = {
                userId: response.id,
                name: response.first_name + " " + response.last_name,
                email: response.email,
                avatarUrl: response.picture.url
            };
            if (response.email === undefined && response.picture.url !== undefined) {
                data = {
                    userId: response.id,
                    name: response.first_name + " " + response.last_name,
                    email: "none",
                    avatarUrl: response.picture.url
                }
            } else if (response.picture.url === undefined && response.email !== undefined) {
                data = {
                    userId: response.id,
                    name: response.first_name + " " + response.last_name,
                    email: response.email,
                    avatarUrl: "none"
                }
            } else if (response.email === undefined && response.picture.url === undefined) {
                data = {
                    userId: response.id,
                    name: response.first_name + " " + response.last_name,
                    email: "none",
                    avatarUrl: "none"
                }
            }
            $.ajax("/facebook-auth",{
                type: 'POST',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify(data),
                success: function (data) {
                    window.location.replace("/home");
                },
                error: function (jqXHR) {
                    user.errorUtils.setErrorsToForm($(form), JSON.parse(jqXHR.responseText));
                }
            });
        });
}