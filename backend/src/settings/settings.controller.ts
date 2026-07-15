import { Controller, Get, Patch, Body, UseGuards, Request } from '@nestjs/common';
import { SettingsService } from './settings.service';
import { UpdateSettingsDto } from './dto/update-settings.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse } from '@nestjs/swagger';

@ApiTags('settings')
@Controller('settings')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class SettingsController {
  constructor(private readonly settingsService: SettingsService) {}

  @Get()
  @ApiOperation({ summary: 'Get current user app preference settings' })
  @ApiResponse({ status: 200, description: 'Success' })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  async getSettings(@Request() req: any) {
    return this.settingsService.getSettingsByUserId(req.user.userId);
  }

  @Patch()
  @ApiOperation({ summary: 'Update system settings (vibration, alerts, sound volume)' })
  @ApiResponse({ status: 200, description: 'Succeeded updating preference settings.' })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  async updateSettings(@Request() req: any, @Body() dto: UpdateSettingsDto) {
    return this.settingsService.updateSettingsByUserId(req.user.userId, dto);
  }
}
