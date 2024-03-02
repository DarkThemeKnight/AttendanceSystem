from fastapi import FastAPI, UploadFile, File, Query, HTTPException,Header
from services import image_processing as ip
import cv2
import face_recognition
from typing import List
from pydantic import BaseModel
import requests
from typing import List
from pydantic import BaseModel
import numpy as np

class EncodedImage(BaseModel):
    message: str
    encoded_image: List[float]  # Representing the data as a list of floats (doubles in Java)


app = FastAPI()

@app.post("/api/v1/image-processing")
async def image_processing(student_id: str = Query(..., title="Student ID"), file: UploadFile = File(...)):
    try:
        image = ip.process_image(file) 
        img = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)       
        face_locations = face_recognition.face_locations(img)
        encode = face_recognition.face_encodings(img, face_locations)
        if(len(encode) != 1):
            return EncodedImage(message= "Invalid Amount of Faces detected", encoded_image = None)
        
        encoded =  EncodedImage(message= "Success", encoded_image= encode[0])
        return encoded
    except Exception as e:
        raise HTTPException(status_code=400, detail="Error processing file")
    
    
class FaceRecognitionRequest(BaseModel):
    matriculation_numbers: List[str]
    encodings : list

@app.post("/api/v1/recognize")
async def recognize_face(authorization: str = Header(None),subject_id: str = Query(..., title="Subject Id") , file: UploadFile = File(...)):
    # to send request to the endpoint ??  to get studnet encodings for this subject id
    header = {
        "Authorization": authorization
    }
    response = requests.get("http://localhost:8080/api/v1/encodings?code="+subject_id,headers= header)
    val = response.json()
    matriculation_numbers = val["matriculation_numbers"]
    encodings = val["encodings"]
    try:
        image = ip.process_image(file)
        image = cv2.resize(image, (265, 240))        
        img_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        face_locations = face_recognition.face_locations(img_rgb)
        face_encodings = face_recognition.face_encodings(img_rgb, face_locations)
        for face_encoding in zip(face_encodings):
            matches = face_recognition.compare_faces(encodings, face_encoding)
            face_distances = face_recognition.face_distance(encodings, face_encoding)
            match_index = np.argmin(face_distances)
            if matches[match_index]:
                print(matriculation_numbers[match_index])
                return {"matriculation_number": matriculation_numbers[match_index]}

    except Exception as e:
            return HTTPException(status_code= 500, detail="Error processing file")

    return {"matriculation_number": None}