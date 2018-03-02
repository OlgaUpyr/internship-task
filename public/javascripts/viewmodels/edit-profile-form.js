var user = user || {};

user.edit_page_form = {
    EditProfileForm: function (id) {
        var self = this;
        self.id = id;
        self.name = ko.observable("");
        self.email = ko.observable("");
        self.current_password = ko.observable("");
        self.new_password = ko.observable("");
        self.confirm_password = ko.observable("");

        $.getJSON("/api/profile", function (data) {
            self.name(data.name);
            self.email(data.email);
        });

        self.submitForm = function (form) {
            var formData = new FormData($("#edit-profile-form")[0]);
            $.ajax({
                url: "/api/profile/edit/" + id,
                type: 'POST',
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
    var model = document.getElementById("edit-profile-form");
    if (model) {
        ko.applyBindings(new user.edit_page_form.EditProfileForm(model.getAttribute('data-id')), model);
    }
});