import { Body, Controller, Get, Post } from '@nestjs/common';
import { CurrentUser } from '../../common/current-user';
import { CurrentUserParam } from '../../common/decorators/current-user.decorator';
import { Public } from '../../common/decorators/public.decorator';
import { AuthService } from './auth.service';
import { ChangePasswordDto } from './dto/change-password.dto';
import { LoginDto } from './dto/login.dto';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { RegisterDto } from './dto/register.dto';

@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Public()
  @Post('login')
  login(@Body() body: LoginDto) {
    return this.authService.login(body);
  }

  @Public()
  @Post('register')
  register(@Body() body: RegisterDto) {
    return this.authService.register(body);
  }

  @Public()
  @Post('refresh')
  refresh(@Body() body: RefreshTokenDto) {
    return this.authService.refresh(body.refreshToken);
  }

  @Get('me')
  me(@CurrentUserParam() user: CurrentUser) {
    return this.authService.me(user);
  }

  @Post('change-password')
  changePassword(@CurrentUserParam() user: CurrentUser, @Body() body: ChangePasswordDto) {
    return this.authService.changePassword(user, body);
  }

  @Post('logout')
  logout() {
    return { success: true };
  }
}
