from app import app
from flask import Flask, request, jsonify
from app import service as ss
import face_recognition
import requests

@app.route('/encode-images/<string:faculty>', methods=['GET'])
def encode_images_route(faculty):
    try:
        images_response_list = ss.encode_images(faculty)
        if images_response_list:
            ids = []
            encode_list = ss.find_encodings(images_response_list, ids)
            ss.save_encodings(encode_list, ids, faculty)
            return jsonify({"message": "Encodings generated successfully."}), 200
        else:
            return jsonify({"error": "No images found for the specified faculty."}), 404
    except Exception as e:
        return jsonify({"error": f"Unexpected error: {e}"}), 500

  

