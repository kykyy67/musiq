package by.aleksandr.music.mapper;

import by.aleksandr.music.dto.response.UserResponse;
import by.aleksandr.music.entity.User;
import java.util.List;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        List<Long> trackIds = user.getTracks() == null
                ? List.of()
                : user.getTracks().stream()
                .map(t -> t.getId())
                .toList();
        return new UserResponse(user.getId(), user.getName(), trackIds);
    }

    public static List<UserResponse> toResponseList(List<User> users) {
        return users.stream().map(UserMapper::toResponse).toList();
    }
}
