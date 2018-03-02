var user = user || {};

user.check_email_form = {
    CheckEmailForm: function () {
        var self = this;
        self.email = ko.observable("");

        self.submitForm = function (form) {
            $.ajax({
                url: "/sendemail",
                type: 'POST',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify({
                    email: self.email()
                }),
                error: function (jqXHR) {
                    user.errorUtils.setErrorsToForm($(form), JSON.parse(jqXHR.responseText));
                }
            })
        }
    }
};

$(function() {
    var model = document.getElementById("check-email-form");
    if (model) {
        ko.applyBindings(new user.check_email_form.CheckEmailForm(), model);
    }
});