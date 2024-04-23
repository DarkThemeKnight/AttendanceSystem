package com.backend.FaceRecognition.services.extras;

import com.backend.FaceRecognition.entities.Schedule;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.repository.ScheduleRepository;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.ScheduleSetupRequest;
import com.backend.FaceRecognition.utils.ScheduleSetupResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final JwtService jwtService;
    private final SubjectService subjectService;
    public ScheduleService(ScheduleRepository scheduleRepository, JwtService jwtService, SubjectService subjectService) {
        this.scheduleRepository = scheduleRepository;
        this.jwtService = jwtService;
        this.subjectService = subjectService;
    }
    public ResponseEntity<ScheduleSetupResponse> fetch(String bearer){
        List<Schedule> schedules = scheduleRepository.findAllByUserId(jwtService.getId(jwtService.extractTokenFromHeader(bearer)));
        return ResponseEntity.ok(parse(schedules));
    }
    private ScheduleSetupResponse parse(List<Schedule> schedules) {
        if (schedules.isEmpty()){
            return new ScheduleSetupResponse();
        }
        return new ScheduleSetupResponse("success",schedules.stream().map(schedule -> ScheduleSetupResponse.CustomRequest.builder()
                .id(schedule.getId())
                .duration(schedule.getDuration())
                .time(schedule.getTime())
                .courseCode(schedule.getCourseCode())
                .courseTitle(schedule.getCourseTitle())
                .dayOfWeek(schedule.getDayOfWeek())
                .build()
        ).toList());
    }
    private Schedule parse(ScheduleSetupRequest.CustomRequest customRequest,String id, String subjectTitle){
        return Schedule.builder()
                .courseTitle(subjectTitle)
                .courseCode(customRequest.getCourseCode())
                .userId(id)
                .duration(customRequest.getDuration())
                .time(customRequest.getTime())
                .dayOfWeek(customRequest.getDayOfWeek())
                .id(customRequest.getId())
                .build();
    }
    private ScheduleSetupResponse.CustomRequest parse(Schedule schedule){
        return  new ScheduleSetupResponse.CustomRequest(schedule.getId(),schedule.getCourseCode(),schedule.getCourseTitle(),schedule.getDuration(),schedule.getTime(),schedule.getDayOfWeek());
    }
    public ResponseEntity<ScheduleSetupResponse> setupMySchedule(ScheduleSetupRequest requests, String bearer){
        String id =  jwtService.getId(jwtService.extractTokenFromHeader(bearer));
        return switch (requests.getUpdateType().toUpperCase()){
            case "UPDATE"->{
                List<ScheduleSetupRequest.CustomRequest> data =requests.getData();
                List<Schedule> mySchedule = scheduleRepository.findAllByUserId(id);
                List<ScheduleSetupResponse.CustomRequest> responseData=new ArrayList<>();
                int i = 0;
                for (Schedule ss:mySchedule){
                    if (ss.getId().equals(data.get(i).getId())){
                        ScheduleSetupRequest.CustomRequest customRequest = data.get(i);
                        i++;
                        Subject subject = subjectService.findSubjectByCode(customRequest.getCourseCode().toUpperCase()).orElse(null);
                        if (subject == null){
                            continue;
                        }
                        Schedule schedule =parse(customRequest,id,subject.getSubjectTitle());
                        schedule = scheduleRepository.save(schedule);
                        ScheduleSetupResponse.CustomRequest response = parse(schedule);                        responseData.add(response);
                    }
                }
                yield ResponseEntity.ok(new ScheduleSetupResponse("Updated Successfully",responseData));
            }
            case "ADD"-> {
                List<ScheduleSetupRequest.CustomRequest> customRequests = requests.getData();
                List<Schedule> mySchedule = customRequests.stream().map(
                        customRequest -> {
                            Subject subject = subjectService.findSubjectByCode(customRequest.getCourseCode().toUpperCase()).orElse(null);
                            if (subject == null){
                                return null;
                            }
                            return Schedule.builder()
                                    .courseTitle(subject.getSubjectTitle())
                                    .courseCode(subject.getSubjectCode())
                                    .userId(id)
                                    .duration(customRequest.getDuration())
                                    .time(customRequest.getTime())
                                    .dayOfWeek(customRequest.getDayOfWeek())
                                    .build();
                        }
                ).filter(Objects::nonNull).toList();
                List<ScheduleSetupResponse.CustomRequest> response = scheduleRepository.saveAll(mySchedule)
                        .stream().map(schedule -> ScheduleSetupResponse.CustomRequest.builder()
                                .id(schedule.getId())
                                .duration(schedule.getDuration())
                                .time(schedule.getTime())
                                .courseCode(schedule.getCourseCode())
                                .courseTitle(schedule.getCourseTitle())
                                .dayOfWeek(schedule.getDayOfWeek())
                                .build()
                        ).toList();
                yield ResponseEntity.ok(new ScheduleSetupResponse("Success",response));
            }
            default -> ResponseEntity.badRequest().body(new ScheduleSetupResponse("Bad type",null));
        };

    }





}
