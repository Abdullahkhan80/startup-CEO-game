import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { UpdateSettingsDto } from './dto/update-settings.dto';

@Injectable()
export class SettingsService {
  constructor(private readonly prisma: PrismaService) {}

  async getSettingsByUserId(userId: string) {
    const settings = await this.prisma.setting.findUnique({
      where: { userId },
    });
    if (!settings) {
      throw new NotFoundException(`Settings for user ${userId} not found.`);
    }
    return settings;
  }

  async updateSettingsByUserId(userId: string, dto: UpdateSettingsDto) {
    return this.prisma.setting.update({
      where: { userId },
      data: dto,
    });
  }
}
