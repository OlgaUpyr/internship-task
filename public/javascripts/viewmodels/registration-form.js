var user = user || {};

user.registration_form = {
    RegistrationForm: function () {
        var self = this;
        self.name = ko.observable("");
        self.email = ko.observable("");
        self.new_password = ko.observable("");
        self.confirm_password = ko.observable("");

        self.submitForm = function (form) {
            var formData = new FormData($("#registration-form")[0]);
            $.ajax({
                url: "/api/registration",
                type: 'POST',
                processData: false,
                contentType: false,
                data: formData,
                error: function (jqXHR) {
                    user.errorUtils.setErrorsToForm($(form), JSON.parse(jqXHR.responseText));
                }
            }).done(function () {
                window.location.replace("/registration");
            });
        }
    }
};

function readUrl(input) {
    if(input.files && input.files[0]){
        var reader = new FileReader();
        reader.onload = function (ev) {
            $('#profileImage').attr('src', ev.target.result);
        };
        reader.readAsDataURL(input.files[0]);
    }
}
$("#file").change(function () {
    readUrl(this);
});

$(function() {
    var model = document.getElementById("registration-form");
    if (model) {
        ko.applyBindings(new user.registration_form.RegistrationForm(), model);
    }
});