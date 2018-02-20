var user = user || {};

user.forgot_password_form = {
    ForgotPasswordForm: function (token) {
        var self = this;
        self.new_password = ko.observable("");
        self.confirm_password = ko.observable("");

        self.submitForm = function (form) {
            $.ajax({
                url: "/reset/" + token,
                type: 'POST',
                dataType: 'text',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify({
                    new_password: self.new_password(),
                    confirm_password: self.confirm_password()
                }),
                error: function (jqXHR) {
                    user.errorUtils.setErrorsToForm($(form), JSON.parse(jqXHR.responseText));
                }
            }).done(function () {
                window.location.replace("/home");
            })
        }
    }
};

$(function() {
    var model = document.getElementById("forgot-password-form");
    if (model) {
        ko.applyBindings(new user.forgot_password_form.ForgotPasswordForm(model.getAttribute('data-token')), model);
    }
});