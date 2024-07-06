from django.urls import path 
from . import views 



app_name = 'App_Cart'


urlpatterns = [
    path('', views.cart, name='cart'),
    path('add_item/<int:pk>/', views.add_to_cart, name='add_item'),
]