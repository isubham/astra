import os
from flask import request, jsonify
from app import db
from utility import Utility
from models import User, License
from sqlalchemy.exc import IntegrityError
from . import routes
from resources import Resources


@routes.route('/auth/signup/', methods=['POST'])
def signup():
    signup_details = request.json
    email, password, license_key = signup_details["email"], signup_details["password"], signup_details["license_key"]

    license_found = db.session.query(License).filter_by(user_email=email).first()
    if license_found is None:
        return jsonify(Resources.error_license_dont_exist())

    if not license_found.license_key.decode() == license_key:
        return jsonify(Resources.error_license_incorrect())

    if not license_found.license_valid(email, os.environ["ASTRA_CODE"], license_key):
        return jsonify(Resources.error_license_invalid())

    user = User(email, Utility.get_hash(password), os.environ["ASTRA_CODE"])
    try:
        db.session.add(user)
        db.session.commit()
        return jsonify({"token" : Utility.create_secret({"id" : user.id}).decode()})
    except IntegrityError as e:
        return jsonify(Resources.error_existing_email())


@routes.route('/auth/signin/', methods=['POST'])
def sign_in():
    sign_in_details = request.json

    user_found = db.session.query(User).filter_by(email=sign_in_details["email"],
                                             password=Utility.get_hash(sign_in_details["password"])).first()
    if not user_found is None:
        user_found.post_signin()
        db.session.commit()
        return jsonify({"token" : Utility.create_secret({"id" : user_found.id}).decode()})

    else:
        return jsonify(Resources.error_email_password_incorrect())


# TODO
# @user_api.route('/auth/forgotpassword/', methods=['POST'])
# @user_api.route('/auth/changepassword/', methods=['POST'])
# @user_api.route('/auth/verifycode/', methods=['POST'])

