package uz.drivesmart.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.drivesmart.dto.request.UserRequestDto;
import uz.drivesmart.dto.request.UserUpdateRequestDto;
import uz.drivesmart.dto.response.UserResponseDto;
import uz.drivesmart.entity.User;


import java.util.List;

/**
 * User entity va DTO o'rtasidagi mapping
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Entity dan Response DTO ga mapping
     */
    @Mapping(target = "fullName", expression = "java(getFullName(user))")
    @Mapping(target = "roleDisplay", expression = "java(user.getRole().getDisplayName())")
    UserResponseDto toResponseDto(User user);

    /**
     * Request DTO dan Entity ga mapping
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    User toEntity(UserRequestDto dto);

    /**
     * Entity list dan Response DTO list ga mapping
     */
    List<UserResponseDto> toResponseDtoList(List<User> users);

    /**
     * Update mapping
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntityFromDto(UserUpdateRequestDto dto, @MappingTarget User user);

    /**
     * To'liq ismni yaratish
     */
    default String getFullName(User user) {
        String fullName = user.getFirstName();
        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            fullName += " " + user.getLastName();
        }
        return fullName;
    }
}