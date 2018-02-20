var user = user || {};

user.errorUtils = {
    setErrorsToForm: function (form, errors) {
        form.find(".form-control").removeClass("is-invalid");
        form.find(".col-form-label").removeClass("text-danger");
        form.find(".invalid-feedback").remove();

        $.each(errors, function (fieldName, errors) {
            if (errors.isEmpty)
                return;
            var field = form.find("[name='"+fieldName+"']");
            var formGroup = field.closest(".form-group");
            var fieldLabel = field.siblings(".col-form-label");

            field.addClass("is-invalid");
            fieldLabel.addClass("text-danger");
            $.each(errors, function (idx, error) {
                formGroup.append('<div class="invalid-feedback">' + error + '</div>')
            });
        })
    }
};