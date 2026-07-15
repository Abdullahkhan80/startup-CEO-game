import { Controller, Get, Post, Delete, Body, UseGuards, Request } from '@nestjs/common';
import { GameSaveService } from './game-save.service';
import { CreateGameSaveDto } from './dto/create-game-save.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse } from '@nestjs/swagger';

@ApiTags('game-saves')
@Controller('game-saves')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class GameSaveController {
  constructor(private readonly gameSaveService: GameSaveService) {}

  @Get('latest')
  @ApiOperation({ summary: 'Load the most recent cloud game state save' })
  @ApiResponse({ status: 200, description: 'Succeeded fetching latest state save.' })
  @ApiResponse({ status: 404, description: 'No game saves found.' })
  @ApiResponse({ status: 401, description: 'Unauthorized credentials.' })
  async getLatestSave(@Request() req: any) {
    return this.gameSaveService.getLatestSave(req.user.userId);
  }

  @Post()
  @ApiOperation({ summary: 'Commit current local game state variables to cloud database' })
  @ApiResponse({ status: 201, description: 'Succeeded saving state parameters.' })
  @ApiResponse({ status: 401, description: 'Unauthorized credentials.' })
  async saveGame(@Request() req: any, @Body() dto: CreateGameSaveDto) {
    return this.gameSaveService.saveGame(req.user.userId, dto);
  }

  @Delete()
  @ApiOperation({ summary: 'Reset game and purge all active cloud game saves' })
  @ApiResponse({ status: 200, description: 'Succeeded purging save logs.' })
  @ApiResponse({ status: 401, description: 'Unauthorized credentials.' })
  async resetGameSaves(@Request() req: any) {
    return this.gameSaveService.deleteSaves(req.user.userId);
  }
}
