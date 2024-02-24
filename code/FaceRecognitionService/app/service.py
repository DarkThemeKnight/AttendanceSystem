from app.Classes import ImagesResponse,EncodeImageRequest
import cv2
import face_recognition
from typing import List
import requests
import numpy as np
from dataclasses import dataclass


def encode_images(faculty:str) -> List[ImagesResponse]:
    url = 'http://localhost:8080/api/v1/images/faculty/'+faculty;
    images_response_list = []
    try:
        response = requests.get(url)
        response.raise_for_status()  # Raise an exception for 4xx or 5xx status codes
        # Assuming the response contains JSON data with the same structure as ImagesResponse
        images_response_data = response.json()
        for image_response in images_response_data:  
            # print(image_response)
            images_list = image_response.get('images', [])
            image_id = image_response.get('id')
            for image_data in images_list:
                images_response_list.append(ImagesResponse(id=image_id, image=image_data))
        return images_response_list
    except requests.exceptions.RequestException as e:
        print(f"Error fetching images from {url}: {e}")

def find_encodings(imgList: List[ImagesResponse], ids: List[str]) -> List[np.ndarray]:
    encodeList = []
    for image_response in imgList:
        matric_num = image_response.id
        byte_array = image_response.image
        np_array = np.frombuffer(byte_array, dtype=np.uint8)
        image_mat = cv2.imdecode(np_array, cv2.IMREAD_COLOR)
        image_mat = cv2.cvtColor(image_mat, cv2.COLOR_BGR2RGB)
        face_encodings = face_recognition.face_encodings(image_mat)        
        if len(face_encodings) > 0:
            encodeList.append(face_encodings[0])
            ids.append(matric_num)
        else:
            print(f"No face found in the image: {matric_num}")

    return encodeList

def save_encodings(encode_list: List[np.ndarray], ids: List[str], file_name: str):
    request_data = EncodeImageRequest(ids=ids, encode=encode_list)
    url = 'http://localhost:8080/api/v1/images/save'
    try:
        response = requests.post(url, json=request_data.__dict__)
        if response.status_code == 200:
            print("Encodings saved successfully.")
        else:
            print(f"Failed to save encodings. Status code: {response.status_code}")    
    except requests.exceptions.RequestException as e:
        print(f"Error sending request to save encodings: {e}")
    
