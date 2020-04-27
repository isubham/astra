import os
from flask_sqlalchemy import SQLAlchemy
from flask_marshmallow import Marshmallow
from flask import Flask, render_template, request, jsonify
from sqlalchemy.exc import IntegrityError
from utility import Utility
from cryptography.fernet import InvalidToken

app = Flask(__name__)

app.config.from_object(os.environ['APP_SETTINGS'])
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)
mb = Marshmallow(app)

from models import User, user_schema, People, person_schema, Activity, activity_schema, License, license_schema

@app.route('/auth/signup/', methods=['POST'])
def signup():
    signup_details = request.json
    user = User(signup_details["email"], Utility.get_hash(signup_details["password"]), os.environ["ASTRA_CODE"])
    try:
        db.session.add(user)
        db.session.commit()
        return user_schema.jsonify(user)
    except IntegrityError as e:
        return user_schema.jsonify(user)


@app.route('/auth/signin/', methods=['POST'])
def sign_in():
    sign_in_details = request.json

    user_found = db.session.query(User).filter_by(email=sign_in_details["email"],
                                             password=Utility.get_hash(sign_in_details["password"])).first()
    if not user_found is None:
        user_found.post_signin()
        db.session.commit()

    return user_schema.jsonify(user_found)



@app.route('/auth/forgotpassword/', methods=['POST'])

@app.route('/auth/changepassword/', methods=['POST'])

@app.route('/auth/verifycode/', methods=['POST'])


@app.route('/person/', methods=['POST'])
def create_people():
    people = request.json
    user = User(people["email"], people["password"], os.environ["ASTRA_CODE"])
    try:
        db.session.add(user)
        db.session.flush()
    except IntegrityError as e:
        return user_schema.jsonify(user)

    people = People(user.id, people["first_name"], people["last_name"], Utility.get_date(people["dob"]),people["profile_pic"],
                    people["id_front"], people["id_back"], people["father_name"], people["username"], people["aadhar_id"])

    try:
        db.session.add(people)
        db.session.commit()
    except IntegrityError as e:
        db.session.rollback()

    return person_schema.jsonify(people)


@app.route('/person/id/<_id>', methods=['GET'])
def get_person(_id):
    person_found = db.session.query(People).filter_by(id=_id).first()
    return person_schema.jsonify(person_found)


@app.route('/person/', methods=['GET'])
def person_search():
    email, dob = request.json["email"], Utility.get_date(request.json["dob"])
    user_found = db.session.query(User).filter_by(email=email).first()

    if not user_found is None:
        person = user_found.People[0]
        if dob == Utility.get_date_from_datetime(str(person.dob)):
            return person_schema.jsonify(person)
        else:
            return jsonify({"message": "Incorrect Date of Birth"})
    else:
        return jsonify({"message": "Incorrect details"})


@app.route('/person/aadhar_id/<aadhar_id>', methods=['GET'])
def person_scan(aadhar_id):
    person_found = db.session.query(People).filter_by(aadhar_id=aadhar_id).first()
    return person_schema.jsonify(person_found)


@app.route('/activity/', methods=['POST'])
def create_activity():
    activity_json = request.json
    user_id, person_id, location, _type = activity_json["user_id"], activity_json['person_id'], \
                                          activity_json["location"], activity_json["type"]
    new_activity = Activity(user_id, person_id, location, _type)
    db.session.add(new_activity)
    db.session.commit()
    return activity_schema.jsonify(new_activity)


# will be exclusively generated by @pitavya
@app.route('/license/', methods=['POST'])
def create_license():
    license_json = request.json
    user_id, app_id, validity = license_json["user_id"], os.environ["ASTRA_CODE"], license_json["validity"]

    try:
        Utility.parseDateYMD(validity)
    except Exception as e:
        return jsonify({"message": "invalid validity"})

    license_exist = db.session.query(License).filter_by(user_id=user_id).first()

    if not license_exist is None:
        return jsonify({"message" : "License exist"})



    try:
        new_license = License(user_id, app_id, validity)
        db.session.add(new_license)
        db.session.commit()
        return license_schema.jsonify(new_license)
    except IntegrityError as e:
        return jsonify({"message" : "user don't exist"})



@app.route('/license/valid/', methods=['POST'])
def validate_license():
    license_json = request.json
    user_id, app_code, license_text = license_json["user_id"], os.environ["ASTRA_CODE"], license_json["license_key"]

    result = db.session.query(License).filter_by(user_id=user_id).first()
    if not result is None:
        try:
            return result.license_valid(user_id, app_code, license_text)
        except InvalidToken as i:
            return jsonify({"message" : "invalid license"})
    else:
        return jsonify({"message" : "user not found"})



@app.route('/', methods=['GET'])
def index():
    return render_template("index.html")


if __name__ == '__main__':
    app.run(debug=True)

