package swd.coiviet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import swd.coiviet.dto.request.CreateUserRequest;
import swd.coiviet.dto.request.UpdateUserRequest;
import swd.coiviet.dto.response.UserResponse;
import swd.coiviet.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(CreateUserRequest dto);

    @Mapping(target = "passwordHash", ignore = true)
    void updateFromDto(UpdateUserRequest dto, @MappingTarget User entity);

    UserResponse toResponse(User user);
}
