var user = user || {};

user.registration_form = {
    RegistrationForm: function () {
        var self = this;
        self.name = ko.observable("");
        self.email = ko.observable("");
        self.new_password = ko.observable("");
        self.confirm_password = ko.observable("");

        self.submitForm = function (form) {
            var formData = new FormData($("#registration-form")[0])
            $.ajax({
                url: "/api/registration",
                type: 'POST',
                headers: { 'IsAjax': 'true' },
                dataType: 'json',
                processData: false,
                contentType: false,
                data: formData,
                error: function (jqXHR) {
                    user.errorUtils.setErrorsToForm($(form), JSON.parse(jqXHR.responseText));
                }
            }).done(function () {
                window.location.replace("/home");
            });
        }
    }
};

$(function() {
    var model = document.getElementById("registration-form");
    if (model) {
        ko.applyBindings(new user.registration_form.RegistrationForm(), model);
    }
});