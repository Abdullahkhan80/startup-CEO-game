import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { RedisService } from '../redis/redis.service';

@Injectable()
export class ConfigurationService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly redis: RedisService,
  ) {}

  private getCacheKey(key: string): string {
    return `config:${key}`;
  }

  async getConfig(key: string): Promise<string> {
    const cacheKey = this.getCacheKey(key);
    
    // Check cache
    const cachedValue = await this.redis.get(cacheKey);
    if (cachedValue) {
      return cachedValue;
    }

    // Check DB
    const dbConfig = await this.prisma.gameConfig.findUnique({
      where: { key },
    });
    
    if (!dbConfig) {
      throw new NotFoundException(`Configuration key ${key} not found.`);
    }

    // Set cache (TTL 1 hour)
    await this.redis.set(cacheKey, dbConfig.value, 3600);

    return dbConfig.value;
  }

  async setConfig(key: string, value: string): Promise<void> {
    const cacheKey = this.getCacheKey(key);

    await this.prisma.gameConfig.upsert({
      where: { key },
      update: { value },
      create: { key, value },
    });

    // Invalidate/update Cache
    await this.redis.set(cacheKey, value, 3600);
  }

  async deleteConfig(key: string): Promise<void> {
    const cacheKey = this.getCacheKey(key);

    await this.prisma.gameConfig.delete({
      where: { key },
    });

    await this.redis.del(cacheKey);
  }
}
