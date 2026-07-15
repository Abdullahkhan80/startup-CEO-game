import { Controller, Get, Patch, Body, UseGuards, Request } from '@nestjs/common';
import { ProfilesService } from './profiles.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse } from '@nestjs/swagger';

@ApiTags('profiles')
@Controller('profiles')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class ProfilesController {
  constructor(private readonly profilesService: ProfilesService) {}

  @Get()
  @ApiOperation({ summary: 'Retrieve the current logged-in user profile' })
  @ApiResponse({ status: 200, description: 'Succeeded fetching user profile.' })
  @ApiResponse({ status: 401, description: 'Unauthorized access.' })
  async getProfile(@Request() req: any) {
    return this.profilesService.getProfileByUserId(req.user.userId);
  }

  @Patch()
  @ApiOperation({ summary: 'Update profile details (CEO, Company name, industry sector)' })
  @ApiResponse({ status: 200, description: 'Succeeded updating profile details.' })
  @ApiResponse({ status: 401, description: 'Unauthorized access.' })
  async updateProfile(@Request() req: any, @Body() dto: UpdateProfileDto) {
    return this.profilesService.updateProfileByUserId(req.user.userId, dto);
  }
}
