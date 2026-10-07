package com.example.rtc

import com.example.data.LiveClassHandRaiseDto
import com.example.data.LiveClassParticipantDto
import com.example.data.LiveClassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveSpeaker(
    val userId: String,
    val name: String,
    val role: String,
    val isSpeaking: Boolean = true
)

/**
 * Manages participant states, speaking permissions, and hand-raise requests.
 */
class ParticipantManager(
    private val classId: String
) {
    private val _participants = MutableStateFlow<List<LiveClassParticipantDto>>(emptyList())
    val participants: StateFlow<List<LiveClassParticipantDto>> = _participants.asStateFlow()

    private val _handRaises = MutableStateFlow<List<LiveClassHandRaiseDto>>(emptyList())
    val handRaises: StateFlow<List<LiveClassHandRaiseDto>> = _handRaises.asStateFlow()

    private val _currentSpeaker = MutableStateFlow<ActiveSpeaker?>(null)
    val currentSpeaker: StateFlow<ActiveSpeaker?> = _currentSpeaker.asStateFlow()

    suspend fun refresh() {
        val list = LiveClassRepository.fetchParticipants(classId)
        _participants.value = list

        val raises = LiveClassRepository.fetchHandRaises(classId)
        _handRaises.value = raises

        // Determine current speaker (teacher if unmuted, or approved speaking student)
        val speakingStudent = list.find { it.role == "student" && !it.isMuted && it.isConnected }
        val teacher = list.find { it.role == "teacher" && it.isConnected }

        _currentSpeaker.value = when {
            speakingStudent != null -> ActiveSpeaker(
                userId = speakingStudent.userId,
                name = speakingStudent.userName,
                role = "student",
                isSpeaking = true
            )
            teacher != null && !teacher.isMuted -> ActiveSpeaker(
                userId = teacher.userId,
                name = teacher.userName,
                role = "teacher",
                isSpeaking = true
            )
            teacher != null -> ActiveSpeaker(
                userId = teacher.userId,
                name = teacher.userName,
                role = "teacher",
                isSpeaking = false
            )
            else -> null
        }
    }

    suspend fun allowStudentToSpeak(studentId: String) {
        LiveClassRepository.handleHandRaise(classId, studentId, "approved")
        LiveClassRepository.updateParticipantState(classId, studentId, isMuted = false, isConnected = true)
        refresh()
    }

    suspend fun muteStudent(studentId: String) {
        LiveClassRepository.handleHandRaise(classId, studentId, "lowered")
        LiveClassRepository.updateParticipantState(classId, studentId, isMuted = true, isConnected = true)
        refresh()
    }

    suspend fun removeStudent(studentId: String) {
        LiveClassRepository.updateParticipantState(classId, studentId, isMuted = true, isConnected = false, removed = true)
        refresh()
    }
}
