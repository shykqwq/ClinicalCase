import { createParamDecorator, ExecutionContext } from '@nestjs/common';
import { CurrentUser } from '../current-user';

export const CurrentUserParam = createParamDecorator(
  (_data: unknown, context: ExecutionContext): CurrentUser => {
    const request = context.switchToHttp().getRequest<{ user: CurrentUser }>();
    return request.user;
  },
);

