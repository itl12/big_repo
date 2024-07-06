from django import forms 
from django.contrib.auth.forms import UserCreationForm
from .models import User, Profile
from django.conf import settings

class SignupForm(UserCreationForm):
    class Meta:
        model = User
        fields = ['email', 'password1', 'password2']


class EditProfileForm(forms.ModelForm):
    class Meta:
        model = Profile
        exclude = ['user']


        