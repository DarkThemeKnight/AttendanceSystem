from typing import List
import numpy as np
from dataclasses import dataclass

@dataclass
class ImagesResponse:
    id: str
    image: bytes

@dataclass
class EncodeImageRequest:
    ids:List[str]
    encode:List[np.ndarray]
    file_name:str