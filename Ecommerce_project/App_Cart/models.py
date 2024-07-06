from django.db import models
from App_Shop.models import Product
from decimal import Decimal
from django.conf import settings

# Create your models here.


class Cart(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, related_name='carts', on_delete=models.CASCADE)
    item = models.ForeignKey(Product, on_delete=models.CASCADE)
    quantity = models.IntegerField(default=1)
    purchased = models.BooleanField(default=False)

    def get_total(self):
        total = Decimal(self.item.price) * self.quantity
        return total.quantize(Decimal('0.01'))

    
    def __str__(self):
        return f'{self.item.name} -->> {self.user} -->> {self.quantity}'