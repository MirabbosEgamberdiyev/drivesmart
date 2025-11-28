package uz.drivesmart.service;

import uz.drivesmart.dto.response.DashboardStatsDto;
import uz.drivesmart.dto.response.TestSessionResponse;
import uz.drivesmart.dto.response.TopicStatsDto;

import java.util.List;

public interface AdminDashboardService {
    DashboardStatsDto getDashboardStats();
    List<TestSessionResponse> getRecentTests(int limit);
    List<TopicStatsDto> getTopicStats();
}