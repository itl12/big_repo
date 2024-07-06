from django.db import models
from django.db.models.signals import post_save
from django.dispatch import receiver

# Create your models here.


class Category(models.Model):
    title = models.CharField(max_length=100)
    created_date = models.DateTimeField(auto_now_add=True)

    class Meta():
        verbose_name_plural = 'Categories'
    def __str__(self) -> str:
        return self.title 
    

class Product(models.Model):
    name = models.CharField(max_length=300)
    category = models.ForeignKey(Category, related_name='products', on_delete=models.CASCADE)
    description = models.TextField(blank=True, null=True)
    price = models.DecimalField(max_digits=10, decimal_places=2)
    old_price = models.DecimalField(max_digits=10, decimal_places=2)
    image = models.ImageField(upload_to='products/', blank=True, null=True)
    created_date = models.DateTimeField(auto_now_add=True)
    update_date = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f'{self.name} --> {self.category}'
    

class Updates(models.Model):
    date = models.DateTimeField(auto_now_add=True)
    item = models.ForeignKey(Product, related_name='updates', on_delete=models.CASCADE)

    class Meta:
        ordering = ['-date']

    def __str__(self) -> str:
        return f'{self.date}  --> {self.item.name}'



@receiver(post_save, sender=Product)
def create_update_object(sender, instance, created, **kwargs):
    Updates.objects.create(item=instance)