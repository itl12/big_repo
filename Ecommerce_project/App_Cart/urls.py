from django.urls import path 
from . import views 



app_name = 'App_Cart'


urlpatterns = [
    path('', views.cart, name='cart'),
    path('add_item/<int:pk>/', views.add_to_cart, name='add_item'),
    path('remove_from_cart/<int:pk>/', views.remove_from_cart, name='remove_from_cart'),
    path('increase_item/<int:pk>/', views.increase_item, name='increase_item'),
    path('decrease_item/<int:pk>/', views.decrease_item, name='decrease_item'),
]