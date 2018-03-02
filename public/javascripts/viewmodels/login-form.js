var user = user || {};

user.login_form = {
    LoginForm: function () {
        var self = this;
        self.email = ko.observable("");
        self.password = ko.observable("");
        self.role = ko.observable("");

        self.submitForm = function (form) {
            $.ajax({
                url: "/api/login",
                type: 'POST',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify({
                    email: self.email(),
                    password: self.password(),
                    role: self.role()
                }),
                error: function (jqXHR) {
                    user.errorUtils.setErrorsToForm($(form), JSON.parse(jqXHR.responseText));
                }
            }).done(function () {
                window.location.replace("/login");
            });
        };
    }
};

$(function() {
    var model = document.getElementById("login-form");
    if (model) {
        ko.applyBindings(new user.login_form.LoginForm(), model);
    }
});