from django.db import models
from App_Shop.models import Product
from django.conf import settings
from decimal import Decimal

# Create your models here.


class Cart(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, related_name='carts', on_delete=models.CASCADE)
    item = models.ForeignKey(Product, on_delete=models.CASCADE)
    quantity = models.IntegerField(default=1)
    purchased = models.BooleanField(default=False)

    def get_total(self):
        return self.item.price * self.quantity

    
    def __str__(self):
        return f'{self.item.name} -->> {self.user} -->> {self.quantity}'
    

class Order(models.Model):
    orderItems = models.ManyToManyField(Cart)
    user = models.ForeignKey(settings.AUTH_USER_MODEL, related_name='order', on_delete=models.CASCADE)
    ordered = models.BooleanField(default=False)

    def get_totals(self):
        total = 0
        # for cart in self.orderItems.all():
        #     total += cart.get_total()
        # return (Decimal(total).quantize(Decimal(0.01)))
        for cart in self.orderItems.all():
            total += cart.get_total()
        return total
    
    def __str__(self):
        return f'{self.user} ordered-->> {self.ordered}'