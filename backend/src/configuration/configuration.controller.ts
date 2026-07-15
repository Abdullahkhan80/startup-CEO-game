import { Controller, Get, Post, Param, Body, UseGuards, Delete } from '@nestjs/common';
import { ConfigurationService } from './configuration.service';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse, ApiBody } from '@nestjs/swagger';

@ApiTags('configuration')
@Controller('configuration')
export class ConfigurationController {
  constructor(private readonly configurationService: ConfigurationService) {}

  @Get(':key')
  @ApiOperation({ summary: 'Retrieve global system game configuration properties' })
  @ApiResponse({ status: 200, description: 'Succeeded fetching config variables' })
  @ApiResponse({ status: 404, description: 'Config key not found' })
  async getConfig(@Param('key') key: string) {
    const value = await this.configurationService.getConfig(key);
    return { key, value };
  }

  @UseGuards(JwtAuthGuard)
  @Post(':key')
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Update or seed a global game configuration property' })
  @ApiResponse({ status: 200, description: 'Succeeded committing configuration update' })
  @ApiResponse({ status: 401, description: 'Unauthorized credentials' })
  @ApiBody({ schema: { type: 'object', properties: { value: { type: 'string' } } } })
  async setConfig(@Param('key') key: string, @Body('value') value: string) {
    await this.configurationService.setConfig(key, value);
    return { success: true };
  }

  @UseGuards(JwtAuthGuard)
  @Delete(':key')
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Remove a global game configuration property' })
  @ApiResponse({ status: 200, description: 'Succeeded purging configuration entry' })
  @ApiResponse({ status: 401, description: 'Unauthorized credentials' })
  async deleteConfig(@Param('key') key: string) {
    await this.configurationService.deleteConfig(key);
    return { success: true };
  }
}
