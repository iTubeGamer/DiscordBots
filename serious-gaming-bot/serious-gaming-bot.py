import discord
from discord import TextChannel, CategoryChannel, Member, Message, RawReactionActionEvent, PartialEmoji
import config
import asyncio
import logging
from typing import Optional

# Constants
class BotConfig:
    TOPIC_CHANNELS_CATEGORY = "Topic Channels"
    SETUP_CHANNEL = "topic-channels"
    CHECKMARK_EMOJI = PartialEmoji(name='✅')
    LOG_FILE = './log/serious-gaming-bot.log'

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(filename=BotConfig.LOG_FILE, encoding='utf-8', mode='w'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger('serious-gaming-bot')

class TopicChannelBot(discord.Client):
    def __init__(self):
        intents = discord.Intents.default()
        intents.message_content = True
        super().__init__(intents=intents)

    async def on_ready(self):
        logger.info(f'Bot is ready and logged in as {self.user}!')
        logger.info(f'Connected to {len(self.guilds)} guilds')

    async def on_raw_reaction_add(self, payload: RawReactionActionEvent):
        """Handle adding users to topic channels when they react with checkmark."""
        try:
            result = await self._handle_reaction(payload, add_access=True)
            if result:
                member, topic_channel = result
                logger.info(f'Added member {member.name} to topic-channel "{topic_channel.name}" '
                          f'on guild "{topic_channel.guild.name}"')
        except Exception as e:
            logger.error(f'Error handling reaction add: {str(e)}', exc_info=True)

    async def on_raw_reaction_remove(self, payload: RawReactionActionEvent):
        """Handle removing users from topic channels when they remove their reaction."""
        try:
            result = await self._handle_reaction(payload, add_access=False)
            if result:
                member, topic_channel = result
                logger.info(f'Removed member {member.name} from topic-channel "{topic_channel.name}" '
                          f'on guild "{topic_channel.guild.name}"')
        except Exception as e:
            logger.error(f'Error handling reaction remove: {str(e)}', exc_info=True)

    async def _handle_reaction(self, payload: RawReactionActionEvent, add_access: bool) -> Optional[tuple[Member, TextChannel]]:
        """Common logic for handling reactions (both add and remove)."""
        if payload.emoji != BotConfig.CHECKMARK_EMOJI:
            return None

        channel = self.get_channel(payload.channel_id)
        if not channel or channel.name.upper() != BotConfig.SETUP_CHANNEL.upper():
            return None

        try:
            guild = channel.guild
            results = await asyncio.gather(
                guild.fetch_member(payload.user_id),
                channel.fetch_message(payload.message_id)
            )
            member, message = results

            topic_channel = await self._get_topic_channel(guild, message.content)
            if topic_channel:
                await topic_channel.set_permissions(member, view_channel=add_access)
                return member, topic_channel
            
            return None

        except discord.NotFound:
            logger.warning(f'Member or message not found for reaction in guild {channel.guild.name}')
            return None
        except discord.Forbidden:
            logger.error(f'Bot lacks permissions to modify channel access in guild {channel.guild.name}')
            return None

    async def _get_topic_channel(self, guild: discord.Guild, channel_name: str) -> Optional[TextChannel]:
        """Find a topic channel by name within the topic channels category."""
        try:
            topic_category = discord.utils.get(
                guild.categories,
                name=BotConfig.TOPIC_CHANNELS_CATEGORY
            )
            
            if not topic_category:
                logger.error(f'Topic channels category not found in guild "{guild.name}"')
                return None

            topic_channel = discord.utils.get(
                topic_category.text_channels,
                name=channel_name.lower()  # Discord channel names are always lowercase
            )

            if not topic_channel:
                logger.error(f'Topic channel "{channel_name}" not found in guild "{guild.name}"')
                return None

            return topic_channel

        except Exception as e:
            logger.error(f'Error finding topic channel: {str(e)}', exc_info=True)
            return None

def main():
    try:
        bot = TopicChannelBot()
        bot.run(config.token)
    except Exception as e:
        logger.critical(f'Failed to start bot: {str(e)}', exc_info=True)

if __name__ == "__main__":
    main()