package com.internship.tool;

import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.entity.User;
import com.internship.tool.repository.ComplianceRecordRepository;
import com.internship.tool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock
    private ComplianceRecordRepository complianceRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataLoader dataLoader;

    // Test 1 — Seeds users when DB is empty
    @Test
    void testRun_SeedsUsers_WhenEmpty() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(complianceRecordRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.saveAll(anyList())).thenReturn(List.of());
        when(complianceRecordRepository.saveAll(anyList())).thenReturn(List.of());

        dataLoader.run();

        verify(userRepository, times(1)).saveAll(anyList());
        verify(complianceRecordRepository, times(1)).saveAll(anyList());
    }

    // Test 2 — Skips seeding users when already exist
    @Test
    void testRun_SkipsUsers_WhenAlreadySeeded() throws Exception {
        when(userRepository.count()).thenReturn(3L);
        when(complianceRecordRepository.count()).thenReturn(15L);

        dataLoader.run();

        verify(userRepository, never()).saveAll(anyList());
        verify(complianceRecordRepository, never()).saveAll(anyList());
    }

    // Test 3 — Seeds records when users exist but records empty
    @Test
    void testRun_SeedsRecords_WhenUsersExistButRecordsEmpty() throws Exception {
        when(userRepository.count()).thenReturn(3L);
        when(complianceRecordRepository.count()).thenReturn(0L);
        when(complianceRecordRepository.saveAll(anyList())).thenReturn(List.of());

        dataLoader.run();

        verify(userRepository, never()).saveAll(anyList());
        verify(complianceRecordRepository, times(1)).saveAll(anyList());
    }

    // Test 4 — Password encoder called for each user
    @Test
    void testRun_PasswordEncoder_CalledForEachUser() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(complianceRecordRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.saveAll(anyList())).thenReturn(List.of());
        when(complianceRecordRepository.saveAll(anyList())).thenReturn(List.of());

        dataLoader.run();

        // 3 users seeded = encode called 3 times
        verify(passwordEncoder, times(3)).encode(anyString());
    }

    // Test 5 — Seeds exactly 15 compliance records
    @Test
    void testRun_Seeds15ComplianceRecords() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(complianceRecordRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.saveAll(anyList())).thenReturn(List.of());

        final List<?>[] capturedList = {null};
        when(complianceRecordRepository.saveAll(anyList())).thenAnswer(inv -> {
            capturedList[0] = inv.getArgument(0);
            return List.of();
        });

        dataLoader.run();

        verify(complianceRecordRepository, times(1)).saveAll(anyList());
        assert capturedList[0] != null;
        assert ((List<?>) capturedList[0]).size() == 15;
    }
}