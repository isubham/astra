import os
from flask_sqlalchemy import SQLAlchemy
from flask_marshmallow import Marshmallow
from flask import Flask, render_template, request, jsonify
import hashlib
from sqlalchemy.exc import IntegrityError
import json

app = Flask(__name__)

app.config.from_object(os.environ['APP_SETTINGS'])
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)
mb = Marshmallow(app)

from models import User, user_schema, Person, person_schema

@app.route('/auth/signup/', methods=['POST'])
def signup():
    signup_details = request.json
    user = User(signup_details["email"], get_hash(signup_details["password"]), os.environ["ASTRA_CODE"])
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
                                             password=get_hash(sign_in_details["password"])).first()
    if user_found is None:
        return jsonify("Email and password don't match", {}, False)
    else:
        user = user_schema.load(user_found)
        user.update_modified_to_current_time()
        db.session.commit()

        return user_schema.jsonify(user_found)



@app.route('/auth/forgotpassword/', methods=['POST'])

@app.route('/auth/changepassword/', methods=['POST'])

@app.route('/auth/verifycode/', methods=['POST'])


@app.route('/person/', methods=['POST'])
def create_people():
    people = request.json
    user = User(people["email"], people["password"], people[os.environ["ASTRA_CODE"]])
    try:
        db.session.add(user)
        db.session.commit()
    except IntegrityError as e:
        return jsonify({"message" : "Email exists", "sucess" : False})

    people = Person(user.id, people["first_name"], people["last_name"], people["profile_pic"],
                    people["id_front"], people["id_back"], people["father_name"], people["username"])

    db.session.add(people)
    db.session.commit()
    return person_schema.jsonify(people)


@app.route('/person/by_id/', methods=['GET'])
def get_person():
    _id = request.args.get("id")
    person_found = db.session.query(Person).filter_by(id=_id).first()
    return person_schema(person_found).jsonify()


@app.route('/person/by_email_and_dob/', methods=['GET'])
def person_search():
    email, dob = request.args.get("email"), request.args.get("dob")
    person_found = db.session.query(Person).filter_by(email=email, dob=dob).first()
    return person_schema(person_found).jsonify()


@app.route('/person/by_aadhar_id/', methods=['GET'])
def person_scan():
    aadhar_id = request.args.get("aadhar_id")
    person_found = db.session.query(Person).filter_by(aadhar_id=aadhar_id).first()
    return person_schema(person_found).jsonify()


def get_hash(data):
    return hashlib.sha3_512(data.encode()).hexdigest()

if __name__ == '__main__':
    app.run(debug=True)

