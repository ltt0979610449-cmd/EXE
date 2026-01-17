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
    User toEntity(CreateUserRequest dto);

    void updateFromDto(UpdateUserRequest dto, @MappingTarget User entity);

    UserResponse toResponse(User user);
}
