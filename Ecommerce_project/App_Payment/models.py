from django.db import models
from django.conf import settings

# Create your models here.


class BillingAddress(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    
    address = models.CharField(max_length=200)
    city = models.CharField(max_length=30)
    zipcode = models.CharField(max_length=30)
    country = models.CharField(max_length=30)

        # check if all fields are filled
    def is_fully_filled(self):
        fields_names = [f.name for f in self._meta.get_fields()]
        for field_name in fields_names:
            value = getattr(self, field_name)
            if value is None or value == '':
                return False
        return True       
    
    def __str__(self):
        return f"{self.user.profile.username}'s billing address."
    
    class Meta:
        verbose_name_plural = "Billing Addresses"