import { IsString, IsOptional, MaxLength } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class UpdateProfileDto {
  @ApiProperty({ example: 'Sarah Jenkins', required: false })
  @IsString()
  @IsOptional()
  @MaxLength(50)
  ceoName?: string;

  @ApiProperty({ example: 'NeuraLinker', required: false })
  @IsString()
  @IsOptional()
  @MaxLength(100)
  companyName?: string;

  @ApiProperty({ example: 'AI', required: false })
  @IsString()
  @IsOptional()
  @MaxLength(30)
  sector?: string;
}
