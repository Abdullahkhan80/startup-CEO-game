import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateGameSaveDto } from './dto/create-game-save.dto';

@Injectable()
export class GameSaveService {
  constructor(private readonly prisma: PrismaService) {}

  private mapSave(save: any) {
    if (!save) return null;
    return {
      ...save,
      usersCount: Number(save.usersCount), // BigInt safe mapping for JSON compatibility
    };
  }

  async saveGame(userId: string, dto: CreateGameSaveDto) {
    // We overwrite or append saves. For this game simulator, we overwrite the latest save to maintain sync,
    // or create a new one if none exists.
    const existingSave = await this.prisma.gameSave.findFirst({
      where: { userId },
      orderBy: { updatedAt: 'desc' },
    });

    if (existingSave) {
      const updated = await this.prisma.gameSave.update({
        where: { id: existingSave.id },
        data: {
          ...dto,
          usersCount: BigInt(dto.usersCount),
        },
      });
      return this.mapSave(updated);
    } else {
      const created = await this.prisma.gameSave.create({
        data: {
          ...dto,
          userId,
          usersCount: BigInt(dto.usersCount),
        },
      });
      return this.mapSave(created);
    }
  }

  async getLatestSave(userId: string) {
    const save = await this.prisma.gameSave.findFirst({
      where: { userId },
      orderBy: { updatedAt: 'desc' },
    });
    if (!save) {
      throw new NotFoundException(`No game saves found for user ${userId}.`);
    }
    return this.mapSave(save);
  }

  async deleteSaves(userId: string) {
    return this.prisma.gameSave.deleteMany({
      where: { userId },
    });
  }
}
