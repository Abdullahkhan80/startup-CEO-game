import { Module } from '@nestjs/common';
import { GameSaveService } from './game-save.service';
import { GameSaveController } from './game-save.controller';

@Module({
  providers: [GameSaveService],
  controllers: [GameSaveController],
  exports: [GameSaveService],
})
export class GameSaveModule {}
