from django.urls import path 
from . import views


app_name = 'App_Payment'


urlpatterns =[
    path('', views.checkout, name='checkout'),

]